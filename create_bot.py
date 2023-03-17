from aiogram import Bot
from aiogram.dispatcher import Dispatcher
from aiogram.contrib.fsm_storage.memory import MemoryStorage

storage = MemoryStorage()

bot = Bot(token='5607543975:AAFeqqH_iYFE_OsrS-nLi76SC-_VJe_GHkU')
dp = Dispatcher(bot, storage=storage)