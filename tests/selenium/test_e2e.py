from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.chrome.options import Options

import time

opts = Options()
opts.add_argument('--headless=new')
opts.add_argument('--no-sandbox')
opts.add_argument('--disable-dev-shm-usage')

service = Service(ChromeDriverManager().install())

def main():
    driver = webdriver.Chrome(service=service, options=opts)
    try:
        driver.get('http://localhost:8080/api/etkinlik/filtre?sehir=İstanbul&tur=Konser')
        time.sleep(1)
        src = driver.page_source
        if src and len(src) > 0:
            print('E2E: Sayfa yüklendi, uzunluk:', len(src))
            exit(0)
        else:
            print('E2E: Sayfa boş')
            exit(2)
    except Exception as ex:
        print('E2E hata:', ex)
        exit(3)
    finally:
        driver.quit()

if __name__ == '__main__':
    main()

