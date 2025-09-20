package org.xhy.community.interfaces.updatelog.request;

public class UpdateUpdateLogRequest extends CreateUpdateLogRequest {

    public UpdateUpdateLogRequest() {
        super();
    }

    public UpdateUpdateLogRequest(String version, String title, String description, java.util.List<CreateChangeRequest> changes) {
        super(version, title, description, changes);
    }
}