from fastapi import status


class VideoServiceException(Exception):
    """Base exception for all service-level errors."""

    def __init__(self, message: str, status_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR):
        self.message = message
        self.status_code = status_code
        super().__init__(message)


class InvalidURLException(VideoServiceException):
    def __init__(self, message: str = "Invalid or unsupported video URL"):
        super().__init__(message, status_code=status.HTTP_400_BAD_REQUEST)


class DownloadFailedException(VideoServiceException):
    def __init__(self, message: str = "Failed to download audio from URL"):
        super().__init__(message, status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)


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
        super().__init__(message, status_code=status.HTTP_422_UNPROCESSABLE_ENTITY)


class TranscriptionException(VideoServiceException):
    def __init__(self, message: str = "Audio transcription failed"):
        super().__init__(message, status_code=status.HTTP_500_INTERNAL_SERVER_ERROR)
