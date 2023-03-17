import sqlite3 as sq
from data_base import Parsers

def sql_start():
    global base, cursor
    try:
        base = sq.connect(f'data_base\menu.db') 
        cursor = base.cursor()
        print("DATA base connected OK!\n")

        ids = read_idUsers()
        for i in range(0, len(ids)):
            delete_table(f"idUser_{ids[i][1]}_order")

        clear_table("idUsers_order")

        delete_table("Dodo_menu")
        delete_table("BK_menu")
        delete_table("Dodo_category")
        delete_table("BK_category")

        create_IdUsers_table()

        create_tables("Dodo")
        create_tables("BK")

        add_data("Dodo", Parsers.DodoPizza_parse())
        add_data("BK", Parsers.BurgerKing_parse())
    except sq.Error as ex:
        print(f"Ошибка - {ex}")


def delete_table(name):
    sqlite_delete_query = f"DROP TABLE IF EXISTS {name}"
    cursor.execute(sqlite_delete_query)
    base.commit()
    print(f"Таблица {name} удалена")

def clear_table(name):
    sqlite_clear_menu_query = f"DELETE FROM {name}"
    cursor.execute(sqlite_clear_menu_query)

    base.commit()
    print(f"Таблицы {name} очищена")


# Меню и категории ресторанов
# -------------------------------------------------------------------------------------
def create_tables(name):
    sqlite_create_menu_table_query = f'''CREATE TABLE IF NOT EXISTS {name}_menu (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                name TEXT NOT NULL,
                                price INTEGER NOT NULL,
                                category TEXT NOT NULL)'''

    sqlite_create_category_table_query = f'''CREATE TABLE IF NOT EXISTS {name}_category (
                                        category TEXT NOT NULL)'''

    cursor.execute(sqlite_create_menu_table_query)
    print(f"Таблица меню {name} создана")

    cursor.execute(sqlite_create_category_table_query)
    print(f"Таблица категорий {name} создана")

    base.commit()


def add_data(name, data):
    categorys = []
    for i in range(len(data)):
        categorys.append(data[i][2])
        cursor.execute(f"INSERT INTO {name}_menu VALUES (?, ?, ?, ?)", tuple([None, data[i][0], data[i][1], data[i][2]]))
    
    category = set(categorys)
    for i in category:
        cursor.execute(f"INSERT INTO {name}_category VALUES (?)", tuple([i]))
    base.commit()
    print(f"Таблица {name} заполнена")

async def read_category(name):
    cursor.execute(f"SELECT * from {name}_category")
    records = cursor.fetchall()
    return records


async def read_menu(name, category):
    cursor.execute(f"SELECT * from {name}_menu WHERE category = '{category}'")
    records = cursor.fetchall()
    return records

async def read_menu_id(name, id):
    cursor.execute(f"SELECT * from {name}_menu WHERE id = {id}")
    records = cursor.fetchone()
    return records
# -------------------------------------------------------------------------------------------------


# Заказ пользователя
# -------------------------------------------------------------------------------------------------
async def create_order_tables(name):
    sqlite_create_order_table_query = f'''CREATE TABLE IF NOT EXISTS idUser_{name}_order (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                name TEXT NOT NULL,
                                price INTEGER NOT NULL,
                                count INTEGER NOT NULL,
                                category TEXT NOT NULL,
                                store TEXT NOT NULL,
                                id_user INTEGER NOT NULL)'''

    cursor.execute(sqlite_create_order_table_query)
    print(f"Таблица заказа {name} создана")

    base.commit()

async def add_order_in_table(name, data):
    cursor.execute(f"INSERT INTO idUser_{name}_order VALUES (?, ?, ?, ?, ?, ?, ?)", tuple([None, data[0], data[1], data[2], data[3], data[4], data[5]]))
    base.commit()
    print(f"Заказ добавлен в таблицу {name}")

async def clear_order_table(name):
    sqlite_clear_menu_query = f"DELETE FROM idUser_{name}_order"
    cursor.execute(sqlite_clear_menu_query)
    base.commit()
    print(f"Таблицы {name} очищена")

async def read_order(name):
    cursor.execute(f"SELECT * from idUser_{name}_order")
    records = cursor.fetchall()
    return records

async def read_order(name, id):
    cursor.execute(f"SELECT * from idUser_{name}_order WHERE id_user = {id}")
    records = cursor.fetchall()
    return records

async def delete_order_table(name):
    sqlite_delete_query = f"DROP TABLE IF EXISTS idUser_{name}_order"
    cursor.execute(sqlite_delete_query)
    base.commit()
    print(f"Таблица idUser_{name}_order удалена")
# -------------------------------------------------------------------------------------------------


# Таблица с ID заказчиков
# -------------------------------------------------------------------------------------------------
def create_IdUsers_table():
    cursor.execute(f'''CREATE TABLE IF NOT EXISTS idUsers_order (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                id_user INTEGER NOT NULL)''')

    print(f"Таблица заказчиков создана")

    base.commit()

async def add_idUser(value):
    cursor.execute(f"INSERT INTO idUsers_order VALUES (?, ?)", tuple([None, value]))
    base.commit()

async def delete_idUser(id):
    cursor.execute(f"DELETE FROM idUsers_order WHERE id_user = {id}")
    base.commit()
    print(f"Из таблицы idUsers_order ID = {id} удалено")

def read_idUsers():
    cursor.execute(f"SELECT * from idUsers_order")
    records = cursor.fetchall()
    return records

async def get_idUsers():
    cursor.execute(f"SELECT * from idUsers_order")
    records = cursor.fetchall()
    return records

async def read_idUser(id):
    cursor.execute(f"SELECT * from idUsers_order WHERE id_user = {id}")
    records = cursor.fetchone()
    return records
# -------------------------------------------------------------------------------------------------