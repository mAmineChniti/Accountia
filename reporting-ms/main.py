from fastapi import FastAPI
from routers import report

app = FastAPI(title="Accountia Reporting")

app.include_router(report.router, prefix="/api/reporting")

@app.get("/")
def root():
    return {"status": "reporting up"}
