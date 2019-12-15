# Scrapper dla [gotowych komputerów na Komputronik.pl](https://www.komputronik.pl/category/5801/komputery-pc.html)

## API
- **GET /computers [ ?s0=1,2,3&s1=1 ]** - lista wszystkich komputerów

Parametry:
- s0, s1 - lista wartości specykacji komputera o identyfikatorze 0 i 1. 

Zwracany model:
```json5
[
  {
    "id": 0,  // Identyfikator komputera na DB
    "name": "Nazwa komputera",
    "price": 111 // Cena w PLN
  },
  {
    "id": 1,  // Identyfikator komputera na DB
    "name": "Nazwa komputera",
    "price": 111 // Cena w PLN
  }
]
```

- **GET /computer?id=?** - specyfikacja pojedynczego komputera

Parametry:
- id - identyfikator komputera z DB

Zwracany model:
```json5
{
  "id": 0,  // Identyfikator komputera na DB
  "name": "Nazwa komputera",
  "price": 111, // Cena w PLN
  "url": "http://komputronik.pl/url.do.komputera",
  "specs": {
    "atr1": "value1"   // Kolejne wartości atrybutów komputera
  }
}
```

- **GET /filters** - lista wszystkich filtrów (elementy specyfikacji + wszystkie możliwe wartości)

Zwracany model:
```json5
[
  {
    "id": 0,   // Identyfikator specyfikacji na DB
    "name": "Nazwa specyfikacji",
    "values": [
      {
        "id": 0, // Identyfikator wartości specyfikacji
        "name": "Nazwa możliwej wartości elementu specyfikacji"
      }
    ] 
  }
]
```