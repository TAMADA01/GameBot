from email import message
from aiogram import types, Dispatcher
from aiogram.dispatcher import FSMContext
from aiogram.dispatcher.filters.state import State, StatesGroup
from aiogram.dispatcher.filters import Text
from aiogram.types import InlineKeyboardButton, InlineKeyboardMarkup

from create_bot import bot, dp
from data_base import Base
from keyboards import kb_client

class Order():
    ID = None
    isMy = None

class FSMClient(StatesGroup):
    order = State()
    orderList = State()
    endOrder = State()
    store = State()
    category = State()
    dish = State()
    count = State()
    final = State()

inline_store_button = InlineKeyboardMarkup(row_width=1).add(
    InlineKeyboardButton(text="Dodo Pizza", callback_data="s_Dodo"),
    InlineKeyboardButton(text="Burger King", callback_data="s_BK")
                                                            )

async def command_start(message : types.Message):
    try:
        await bot.send_message(message.from_user.id, "Start", reply_markup=kb_client)
        await message.delete()
    except:
        await message.reply("Общение с ботом через ЛС, @OrderCollectorByAxelBot")



async def start_order_command(message : types.Message):
    await FSMClient.order.set()
    await bot.send_message(message.from_user.id, 
                        text="Начало заказа",
                        reply_markup=InlineKeyboardMarkup(row_width=1).add(
                            InlineKeyboardButton(text="Создать свой заказ", callback_data=f"id_{message.from_user.id}"),
                            InlineKeyboardButton(text="Присоедениться к заказу", callback_data="con_"),
                            InlineKeyboardButton(text="Отмена", callback_data="back_")
                        ))


async def active_order_command(callback : types.CallbackQuery):
    await FSMClient.orderList.set()

    inline_order_button = InlineKeyboardMarkup(row_width=1)

    orders = await Base.get_idUsers()
    for order in orders:
        inline_order_button.add(InlineKeyboardButton(text=f"{order[1]}", callback_data=f"id_s_{order[0]}"))

    inline_order_button.add(InlineKeyboardButton(text=f"Назад", callback_data=f"back_"))

    await bot.edit_message_text(chat_id=callback.from_user.id,
                                message_id=callback.message.message_id, 
                                text=f"Активные заказы", 
                                reply_markup=inline_order_button)

async def registe_id_order_command(callback : types.CallbackQuery):
    data = callback.data.split("_")
    if data[1] == "s":
        Order.ID = int(data[2])
        Order.isMy = False
    else:
        Order.ID = int(data[1])
        Order.isMy = True
        await Base.add_idUser(Order.ID)
        await Base.create_order_tables(f"{Order.ID}")
        await Base.clear_order_table(f"{Order.ID}")
    
    await order_num_edit_command(callback)

inline_order_button = InlineKeyboardMarkup(row_width=1).add(
                                            InlineKeyboardButton(text="Добавить", callback_data="o_"),
                                            InlineKeyboardButton(text="Список", callback_data="dataOr_"),
                                            InlineKeyboardButton(text="Завершить", callback_data="end_")
                                        )

async def order_num_edit_command(callback : types.CallbackQuery):
    await FSMClient.store.set()
    await bot.edit_message_text(chat_id=callback.from_user.id,
                            message_id=callback.message.message_id, 
                            text=f"Заказ номер: {Order.ID}", 
                            reply_markup=inline_order_button)


async def order_num_command(callback : types.CallbackQuery):
    await FSMClient.store.set()
    await bot.send_message(chat_id=callback.from_user.id, 
                            text=f"Заказ номер: {Order.ID}", 
                            reply_markup=inline_order_button)


async def list_order_command(callback : types.CallbackQuery):
    order = await Base.read_order(f"{Order.ID}", callback.from_user.id)
    
    mes = f"Список вашего заказа в заказе: {Order.ID}\n\n"
    num = 0
    for pos in order:
        num += 1
        mes += f"\t{num}) Название: {pos[1]}\n\tСтоимость: {pos[2]}\n\tКоличество: {pos[3]} \n\n"
    await bot.edit_message_text(chat_id=callback.from_user.id, 
                                message_id=callback.message.message_id, 
                                text=mes,
                                reply_markup=InlineKeyboardMarkup().add(
                                    InlineKeyboardButton(text="К заказу", callback_data="start_")
                                ))


async def end_order_command(callback : types.CallbackQuery):
    await FSMClient.endOrder.set()
    await bot.edit_message_text(chat_id=callback.from_user.id, 
                                message_id=callback.message.message_id, 
                                text="Завершение заказа:",
                                reply_markup=InlineKeyboardMarkup(row_width=1).add(
                                    InlineKeyboardButton(text="Сохранить заказ", callback_data="save_"),
                                    InlineKeyboardButton(text="Отменить заказ", callback_data="del_"),
                                    InlineKeyboardButton(text="Назад", callback_data="back_")
                                ))


async def save_order_command(callback : types.CallbackQuery):
    pass


async def del_order_command(callback : types.CallbackQuery, state : FSMContext):

    await Base.delete_idUser(Order.ID)
    await Base.delete_order_table(Order.ID)

    await bot.delete_message(message_id=callback.message.message_id,
                                chat_id=callback.from_user.id)
    await bot.send_message(text="Заказ отменен",
                            chat_id=callback.from_user.id)
    await state.finish()


async def choose_store_command(callback : types.CallbackQuery):
    await FSMClient.category.set()
    await bot.edit_message_text(chat_id=callback.from_user.id, 
                                message_id=callback.message.message_id, 
                                text=f"Выбирите магазин в котором хотите заказать", 
                                reply_markup=inline_store_button)

# @dp.callback_query_handler(lambda x: x.data and x.data.startswith("store_"), state=FSMClient.store)
async def choose_category_command(callback : types.CallbackQuery, state : FSMContext):
    await FSMClient.dish.set()
    inline_category_button = InlineKeyboardMarkup(row_width=1)
    async with state.proxy() as data:
        data["store"] = callback.data.split('_')[1]
    categorys = await Base.read_category(f"{callback.data.split('_')[1]}")
    for category in categorys:
        inline_category_button.add(InlineKeyboardButton(text=f'{category[0]}', callback_data=f"c_{category[0]}"))
    inline_category_button.add(InlineKeyboardButton(text='Отмена', callback_data=f"back_{callback.data.split('_')[1]}"))
    await bot.edit_message_text(chat_id=callback.from_user.id, 
                                message_id=callback.message.message_id, 
                                text=f"Выбирите категорию '{callback.data.split('_')[1]}':", 
                                reply_markup=inline_category_button)

# @dp.callback_query_handler(lambda x: x.data and x.data.startswith("category_"), state=FSMClient.category)
async def choose_dish_command(callback : types.CallbackQuery, state : FSMContext):
    await FSMClient.count.set()
    async with state.proxy() as data:
        data['category'] = callback.data.split('_')[1]
        dishs = await Base.read_menu(f"{data['store']}", f"{data['category']}")
    inline_dish_button = InlineKeyboardMarkup(row_width=1)

    for dish in dishs:
        inline_dish_button.add(InlineKeyboardButton(text=f'{dish[1]} {dish[2]}рублей', callback_data=f"d_{dish[0]}"))

    async with state.proxy() as data:
        inline_dish_button.add(InlineKeyboardButton(text='Отмена', callback_data=f"back_{data['store']}"))
        await bot.edit_message_text(chat_id=callback.from_user.id, 
                                message_id=callback.message.message_id, 
                                text=f"Выбирите блюдо из категории '{data['category']}' ресторана {data['store']}:", 
                                reply_markup=inline_dish_button)


async def choose_count_command(callback : types.CallbackQuery, state : FSMContext):
    await FSMClient.final.set()
    async with state.proxy() as data:
        if callback.data.startswith("cnt_"):
            data['count'] = callback.data.split('_')[1]
        else:
            data["count"] = 1
            data['dish_id'] = callback.data.split('_')[1]

        inline_count_button = InlineKeyboardMarkup(row_width=1)
        inline_count_button.row(InlineKeyboardButton(text="-", callback_data=f"cnt_{int(data['count'])-1}"),
                                InlineKeyboardButton(text=f"{data['count']}", callback_data="0"),
                                InlineKeyboardButton(text="+", callback_data=f"cnt_{int(data['count'])+1}")
                                )

        inline_count_button.add(InlineKeyboardButton(text="Выбрать", callback_data="fin_"))

        await bot.edit_message_text(chat_id=callback.from_user.id, 
                                    message_id=callback.message.message_id, 
                                    text=f"Выбирите количество:", 
                                    reply_markup=inline_count_button)

# @dp.callback_query_handler(lambda x: x.data and x.data.startswith("dish_"), state=FSMClient.dish)
async def add_dish_in_data_command(callback : types.CallbackQuery, state : FSMContext):
    async with state.proxy() as data:
        dish = await Base.read_menu_id(data["store"], data["dish_id"])
        await bot.edit_message_text(chat_id=callback.from_user.id, 
                                    message_id=callback.message.message_id, 
                                    text=f"Блюдо выбрано: {data['store']} - {data['category']} - {dish[1]} стоимостью: {dish[2]}")
        order = [dish[1], dish[2], data["count"], data["category"], data["store"], callback.from_user.id]
        await Base.add_order_in_table(f"{Order.ID}", order)
    await state.finish()
    await order_num_command(callback)

async def go_previous_command(callback : types.CallbackQuery, state : FSMContext):
    # await FSMClient.previous()

    current_state = await state.get_state()
    if current_state == FSMClient.order.state:
        await bot.delete_message(message_id=callback.message.message_id,
                                chat_id=callback.from_user.id)
        await bot.send_message(text="Отмена",
                            chat_id=callback.from_user.id)
        await state.finish()

    elif current_state == FSMClient.orderList.state:
        await bot.delete_message(message_id=callback.message.message_id,
                                chat_id=callback.from_user.id)
        await start_order_command(callback)

    elif current_state == FSMClient.endOrder.state:
        await order_num_edit_command(callback) 

    elif current_state == FSMClient.category.state:
        await choose_store_command(callback) 

    elif current_state == FSMClient.dish.state:
        await choose_store_command(callback, state)

    elif current_state == FSMClient.count.state:
        await choose_category_command(callback, state)
    else:
        await bot.send_message(text=f"{current_state}",
                            chat_id=callback.from_user.id)




def register_handlers_client(dp : Dispatcher):
    dp.register_message_handler(command_start, commands=['start', 'help'])

    dp.register_message_handler(start_order_command, commands=['заказать'], state=None)
    dp.register_message_handler(start_order_command, Text(equals='заказать', ignore_case=True), state=None)
    
    dp.register_callback_query_handler(go_previous_command, lambda x: x.data and x.data.startswith("back_"), state=FSMClient.all_states)

    dp.register_callback_query_handler(active_order_command, lambda x: x.data and x.data.startswith("con_"), state=FSMClient.order)
    dp.register_callback_query_handler(registe_id_order_command, lambda x: x.data and x.data.startswith("id_"), state=FSMClient.order)

    dp.register_callback_query_handler(list_order_command, lambda x: x.data and x.data.startswith("dataOr_"), state=FSMClient.all_states)
    dp.register_callback_query_handler(end_order_command, lambda x: x.data and x.data.startswith("end_"), state=FSMClient.all_states)

    dp.register_callback_query_handler(order_num_edit_command, lambda x: x.data and x.data.startswith("start_"), state=FSMClient.store)
    dp.register_callback_query_handler(del_order_command, lambda x: x.data and x.data.startswith("del_"), state=FSMClient.endOrder)

    dp.register_callback_query_handler(choose_store_command, lambda x: x.data and x.data.startswith("o_"), state=FSMClient.store)
    dp.register_callback_query_handler(choose_category_command, lambda x: x.data and x.data.startswith("s_"), state=FSMClient.category)
    dp.register_callback_query_handler(choose_dish_command, lambda x: x.data and x.data.startswith("c_"), state=FSMClient.dish)
    dp.register_callback_query_handler(choose_count_command, lambda x: x.data and x.data.startswith("d_"), state=FSMClient.count)
    dp.register_callback_query_handler(choose_count_command, lambda x: x.data and x.data.startswith("cnt_"), state=FSMClient.final)
    dp.register_callback_query_handler(add_dish_in_data_command, lambda x: x.data and x.data.startswith("fin_"), state=FSMClient.final)