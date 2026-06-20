from typing import List, Optional

from pydantic import BaseModel, field_validator

_VALID_STYLES = {"short", "detailed", "exam", "bullets"}

class URLRequest(BaseModel):
    url: str
    summary_style: str = "detailed"

    @field_validator("url")
    @classmethod
    def url_must_not_be_empty(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("URL cannot be empty")
        return v.strip()

    @field_validator("summary_style")
    @classmethod
    def style_must_be_valid(cls, v: str) -> str:
        normalised = v.strip().lower()
        if normalised not in _VALID_STYLES:
            return "detailed"
        return normalised

class ImportantTerm(BaseModel):
    term: str
    meaning: str

class StudyNoteSection(BaseModel):
    heading: str
    points: List[str]

class NotesOutput(BaseModel):
    summary: str
    key_points: List[str]
    important_terms: List[ImportantTerm]
    study_notes: List[StudyNoteSection]
    possible_questions: List[str]

class VideoNotesResponse(BaseModel):
    success: bool
    source_type: str                    
    title: str
    transcript: str
    notes: NotesOutput

class ErrorResponse(BaseModel):
    success: bool = False
    error: str
    detail: Optional[str] = None

class HealthResponse(BaseModel):
    status: str
    service: str
