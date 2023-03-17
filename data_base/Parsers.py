from bs4 import BeautifulSoup
from data_base import source

def DodoPizza_parse():
    url = "https://dodopizza.ru/dubna"

    data = []
    # source.get_source("Dodo", url)
    soup = BeautifulSoup(source.give_sourse("Dodo"), 'html.parser')
    main = soup.find("main")
    sections = main.find_all("section")
    categories = main.find_all("h2")
    for i in range(len(sections)):
        articles = sections[i].find_all("article")
        for article in articles:
            name = article.find("div", attrs ={"data-gtm-id" : "product-title"})
            price = article.find("div", attrs ={"class" : "product-control-price"})
            if name != None:
                data.append([name.text, price.text[:len(price.text)-2], categories[i].text])
    return data

def BurgerKing_parse():
    url = "https://burgerkingrus.ru/category/2"

    data = []
    # source.get_source("BK", url)
    soup = BeautifulSoup(source.give_sourse("BK"), 'html.parser')
    cards = soup.find_all("div", attrs={"class" : "bk-dish-card"})
    for card in cards:
        name = card.find("div", attrs={"class" : "bk-dish-card__title"})
        price = card.find("div", attrs={"class" : "bk-price"})
        newPrice = ""
        for c in price.text:
            if c != ' ' and c != '\n':
                newPrice += c
        data.append([name.text, newPrice, "Бургеры"])
    return data