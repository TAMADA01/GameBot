from aiogram import types, Dispatcher

from create_bot import bot, dp

async def command_start(message : types.Message):
    try:
        await bot.send_message(message.chat.id, "Start")
        await message.delete()
    except:
        await message.reply("Общение с ботом только в чате, @OrderCollectorByAxelBot")

async def command_show_info_character(message : types.Message):
    image = types.InputFile("Wznew7dTfVcGKyZ5Nxc5.png")
    await bot.send_photo(message.chat.id, photo=image, caption="info")


def register_handlers_client(dp : Dispatcher):
    dp.register_message_handler(command_start, commands=['start', 'help'])
    dp.register_message_handler(command_show_info_character, commands=['show'])