from pydantic_settings import BaseSettings
from pydantic import Field
from pydantic_extra_types import PostgresDsn


class Settings(BaseSettings):
    # Primary options: accept a full DATABASE_URL or individual components
    DATABASE_URL: PostgresDsn | None = None

    POSTGRES_USER: str = Field(..., env='POSTGRES_USER')
    POSTGRES_PASSWORD: str = Field(..., env='POSTGRES_PASSWORD')
    POSTGRES_HOST: str = Field('postgres', env='POSTGRES_HOST')
    POSTGRES_PORT: int = Field(5432, env='POSTGRES_PORT')
    POSTGRES_DB: str = Field(..., env='POSTGRES_DB')

    class Config:
        env_file = '.env'
        env_file_encoding = 'utf-8'

    @property
    def database_url(self) -> str:
        if self.DATABASE_URL:
            return str(self.DATABASE_URL)
        return f"postgresql://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"


settings = Settings()
