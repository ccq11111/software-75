package com.example.software.api;

/**
 * Response for import operations
 */
public class ImportResponse extends ApiResponse {
    private int entriesImported;
    private int entriesSkipped;
    
    public ImportResponse() {
        super();
    }
    
    public ImportResponse(boolean success, String message) {
        super(success, message);
    }
    
    public ImportResponse(int entriesImported, int entriesSkipped) {
        super(true, "Import successful");
        this.entriesImported = entriesImported;
        this.entriesSkipped = entriesSkipped;
    }
    
    public int getEntriesImported() {
        return entriesImported;
    }
    
    public void setEntriesImported(int entriesImported) {
        this.entriesImported = entriesImported;
    }
    
    public int getEntriesSkipped() {
        return entriesSkipped;
    }
    
    public void setEntriesSkipped(int entriesSkipped) {
        this.entriesSkipped = entriesSkipped;
    }
}
