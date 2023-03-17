from selenium import webdriver

import time

def get_source(name, url):
    options = webdriver.ChromeOptions() 
    try:
        driver = webdriver.Chrome(
            executable_path="Driver\chromedriver.exe",
            options=options
        )
        driver.get(url)
        time.sleep(5)

        source = driver.page_source

        with open(f"Html\{name}.html", "w", encoding='utf-8') as file:
            file.write(source)
    except Exception as ex:
        print(ex)
    finally:
        driver.close()
        driver.quit()

def give_sourse(name):
    with open(f"Html\{name}.html", encoding="utf-8") as file:
        return file.read()