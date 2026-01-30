from sqlalchemy import Column, Integer, String, Date, Float
from database import Base

class Report(Base):
    __tablename__ = 'reports'
    id = Column(Integer, primary_key=True, index=True)
    tenant_id = Column(String, index=True)
    name = Column(String)
    value = Column(Float)
from sqlalchemy import Column, Integer, String, Date, Float
from database import Base

class Report(Base):
    __tablename__ = 'reports'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    value = Column(Float)
