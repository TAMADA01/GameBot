import asyncio

from aiogram.utils import executor
from create_bot import dp
from data_base import Base

async def on_startup(_):
    Base.sql_start()
    print("Бот вошел в онлайн")

from handler import client, admin, other 

client.register_handlers_client(dp)

if __name__ == "__main__":
    # loop = asyncio.get_event_loop()
    # loop.create_task(admin.update_db())
    executor.start_polling(dp, skip_updates=True, on_startup=on_startup)