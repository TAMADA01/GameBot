import time
import asyncio
from create_bot import bot, dp

async def update_db():
    while True:
        print("Hello")
        await asyncio.sleep(10)
