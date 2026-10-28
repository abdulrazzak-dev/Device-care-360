package com.devicecare360.technician.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredDocument {
    private String url;
    private String publicId;
    private String filename;
    private long sizeBytes;
    private String mimeType;
}
