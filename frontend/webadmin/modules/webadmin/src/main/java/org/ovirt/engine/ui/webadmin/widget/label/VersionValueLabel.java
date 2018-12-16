package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.webadmin.widget.renderer.VersionRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class VersionValueLabel extends ValueLabel<RpmVersion> {

    public VersionValueLabel() {
        super(new VersionRenderer());
    }

}
