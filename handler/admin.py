import time
import asyncio
from data_base import Base
from create_bot import bot, dp

async def update_db():
    while True:
        print("Hrllo")
        await asyncio.sleep(10)
    # Base.sql_start()
