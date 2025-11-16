package uploader.model;

public enum MessageStatus {
    UPLOADED,
    SENT_TO_ROUTER,
    RECEIVED,
    DELIVERED,
    FAILED_UPLOAD,
    FAILED_ROUTER,
    UNKNOWN
}
