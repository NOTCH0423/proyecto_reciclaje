package com.reciclaje.api;

public class APIKey {
    private String id;
    private String created_at;
    private CreatedBy created_by;
    private String name;
    private String partial_key_hint;
    private String status;
    private String type;
    private String workspace_id;

    public static class CreatedBy {
        private String id;
        private String type;

        public String getId() { return id; }
        public String getType() { return type; }
        public void setId(String id) { this.id = id; }
        public void setType(String type) { this.type = type; }
        @Override
        public String toString() { return "CreatedBy{id='"+id+"', type='"+type+"'}"; }
    }

    public String getId() { return id; }
    public String getCreated_at() { return created_at; }
    public CreatedBy getCreated_by() { return created_by; }
    public String getName() { return name; }
    public String getPartial_key_hint() { return partial_key_hint; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public String getWorkspace_id() { return workspace_id; }

    public void setId(String id) { this.id = id; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setCreated_by(CreatedBy created_by) { this.created_by = created_by; }
    public void setName(String name) { this.name = name; }
    public void setPartial_key_hint(String partial_key_hint) { this.partial_key_hint = partial_key_hint; }
    public void setStatus(String status) { this.status = status; }
    public void setType(String type) { this.type = type; }
    public void setWorkspace_id(String workspace_id) { this.workspace_id = workspace_id; }

    @Override
    public String toString() {
        return "APIKey{" +
                "id='" + id + '\'' +
                ", created_at='" + created_at + '\'' +
                ", created_by=" + created_by +
                ", name='" + name + '\'' +
                ", partial_key_hint='" + partial_key_hint + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", workspace_id='" + workspace_id + '\'' +
                '}';
    }
}
