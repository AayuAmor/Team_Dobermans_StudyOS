from typing import List, Optional

from pydantic import BaseModel, field_validator


class URLRequest(BaseModel):
    url: str

    @field_validator("url")
    @classmethod
    def url_must_not_be_empty(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("URL cannot be empty")
        return v.strip()


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
    source_type: str  # "url" | "upload"
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
