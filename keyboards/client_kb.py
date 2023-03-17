from ctypes import resize
from aiogram.types import KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove

b1 = KeyboardButton("Заказать")

kb_client = ReplyKeyboardMarkup(resize_keyboard=True)
kb_client.add(b1)