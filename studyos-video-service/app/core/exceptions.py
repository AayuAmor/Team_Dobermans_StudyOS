from fastapi import status

class VideoServiceException(Exception):

    def __init__(self, message: str, status_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR):
        self.message = message
        self.status_code = status_code
        super().__init__(message)

class InvalidURLException(VideoServiceException):
    def __init__(self, message: str = "Invalid or unsupported video URL"):
        super().__init__(message, status_code=status.HTTP_400_BAD_REQUEST)

class VideoUnavailableException(VideoServiceException):
    def __init__(self, message: str = "Video is unavailable, private, or has been removed."):
        super().__init__(message, status_code=status.HTTP_404_NOT_FOUND)

class VideoBlockedException(VideoServiceException):
    def __init__(self, message: str = "This video cannot be downloaded — it may be restricted or region-blocked."):
        super().__init__(message, status_code=status.HTTP_403_FORBIDDEN)

class VideoAgeRestrictedException(VideoServiceException):
    def __init__(self, message: str = "Age-restricted videos cannot be processed without authentication."):
        super().__init__(message, status_code=status.HTTP_451_UNAVAILABLE_FOR_LEGAL_REASONS)

class RateLimitedException(VideoServiceException):
    def __init__(self, message: str = "Rate limit reached from the video provider. Please wait a moment and try again."):
        super().__init__(message, status_code=status.HTTP_429_TOO_MANY_REQUESTS)

class GeoBlockedException(VideoServiceException):
    def __init__(self, message: str = "This video is not available in the server's region."):
        super().__init__(message, status_code=status.HTTP_451_UNAVAILABLE_FOR_LEGAL_REASONS)

class DownloadFailedException(VideoServiceException):
    def __init__(self, message: str = "Failed to download audio from URL"):
        super().__init__(message, status_code=status.HTTP_422_UNPROCESSABLE_CONTENT)

class UnsupportedFileTypeException(VideoServiceException):
    def __init__(self, message: str = "Unsupported file type"):
        super().__init__(message, status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE)

class FileTooLargeException(VideoServiceException):
    def __init__(self, max_mb: int):
        super().__init__(
            f"File exceeds maximum allowed size of {max_mb} MB",
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
        )

class AudioExtractionException(VideoServiceException):
    def __init__(self, message: str = "Failed to extract audio from video file"):
        super().__init__(message, status_code=status.HTTP_422_UNPROCESSABLE_CONTENT)

class TranscriptionException(VideoServiceException):
    def __init__(self, message: str = "Audio transcription failed"):
        super().__init__(message, status_code=status.HTTP_500_INTERNAL_SERVER_ERROR)
