package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class DownloadImageHandler {

    private static final Logger log = Logger.getLogger(DownloadImageHandler.class.getName());

    private DiskImage diskImage;

    public DownloadImageHandler(DiskImage diskImage) {
        this.diskImage = diskImage;
    }

    private TransferDiskImageParameters createInitParams() {
        TransferDiskImageParameters parameters = new TransferDiskImageParameters();
        parameters.setTransferType(TransferType.Download);
        parameters.setImageId(diskImage.getId());
        parameters.setTransferSize(diskImage.getActualSizeInBytes());
        String fileExtension = diskImage.getVolumeFormat() == VolumeFormat.COW ?
                ".qcow2" : ".raw"; //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setDownloadFilename(diskImage.getDiskAlias() + fileExtension); //$NON-NLS-1$
        return parameters;
    }

    public void start() {
        Frontend.getInstance().runAction(ActionType.TransferDiskImage,
                createInitParams(),
                result -> {
                    if (result.getReturnValue().getSucceeded()) {
                        Guid transferId = result.getReturnValue().getActionReturnValue();
                        Frontend.getInstance().runQuery(QueryType.GetImageTransferById,
                                new IdQueryParameters(transferId),
                                new AsyncQuery<QueryReturnValue>(returnValue -> {
                                    ImageTransfer imageTransfer = returnValue.getReturnValue();
                                    initiateDownload(imageTransfer);
                                }));
                    }
                },
                this);
    }

    public void stop() {
        closeSession(ImageTransferPhase.FINALIZING_SUCCESS, null);
    }

    private void initiateDownload(ImageTransfer imageTransfer) {
        String url = imageTransfer.getProxyUri() + "/" + imageTransfer.getImagedTicketId(); //$NON-NLS-1$

        log.info("Initiating download: " + url); //$NON-NLS-1$

        String sessionsUrl = imageTransfer.getProxyUri().replace(
                "images", "sessions/"); //$NON-NLS-1$ //$NON-NLS-2$
        startSession(sessionsUrl, imageTransfer, sessionId -> {
            if (sessionId == null) {
                logError(url);
                return;
            }

            // Invoke download
            String params = "?session_id=" + sessionId; //$NON-NLS-1$
            Frame frame = new Frame(url + params);
            frame.addLoadHandler(loadEvent -> Scheduler.get().scheduleDeferred(() ->
                    RootPanel.get().remove(frame)));
            frame.getElement().getStyle().setDisplay(Style.Display.NONE);
            RootPanel.get().add(frame);
        });
    }

    private void startSession(String url, ImageTransfer imageTransfer, final AsyncCallback<String> callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);
        requestBuilder.setHeader("Authorization", imageTransfer.getSignedTicket()); //$NON-NLS-1$
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                    callback.onSuccess(null);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    try {
                        String sessionId = response.getHeader("Session-Id"); //$NON-NLS-1$
                        callback.onSuccess(sessionId);
                    } catch (IllegalArgumentException ex) {
                        callback.onSuccess(null);
                    }
                }
            });
        } catch (RequestException ignore) {
            callback.onSuccess(null);
        }
    }

    private void closeSession(ImageTransferPhase imageTransferPhase, AuditLogType auditLogType) {
        ImageTransfer updates = new ImageTransfer();
        updates.setPhase(imageTransferPhase);
        TransferImageStatusParameters parameters = new TransferImageStatusParameters();
        parameters.setDiskId(diskImage.getId());
        parameters.setUpdates(updates);
        if (auditLogType != null) {
            parameters.setAuditLogType(auditLogType);
        }

        Frontend.getInstance().runAction(ActionType.TransferImageStatus, parameters);
    }

    private void logError(String url) {
        log.info("Failed starting session from: " + url); //$NON-NLS-1$
        closeSession(ImageTransferPhase.FINALIZING_FAILURE, AuditLogType.DOWNLOAD_IMAGE_NETWORK_ERROR);
    }

    public static boolean isDownloadAllowed(List<? extends Disk> disks) {
        return disks != null && !disks.isEmpty() && disks.stream()
                .allMatch((Predicate<Disk>) disk ->
                        disk instanceof DiskImage
                        && disk.getImageTransferPhase() == null
                        && ((DiskImage) disk).getImageStatus() == ImageStatus.OK
                        && ((DiskImage) disk).getActualSizeInBytes() > 0
                        && ((DiskImage) disk).getParentId().equals(Guid.Empty));
    }

    public static boolean isStopDownloadAllowed(List<? extends Disk> disks) {
        return disks != null && !disks.isEmpty() && disks.stream()
                .allMatch((Predicate<Disk>) disk ->
                        disk instanceof DiskImage
                        && disk.getImageTransferPhase() == ImageTransferPhase.TRANSFERRING
                        && !UploadImageManager.getInstance().isUploadImageHandlerExists(disk.getId()));
    }
}
