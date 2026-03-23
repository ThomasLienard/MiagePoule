const today = new Date()
const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000)
const tomorrowTomorrow = new Date(tomorrow.getTime() + 24 * 60 * 60 * 1000)

export const agendaWithAllEvents = [
    {
        "id": 1,
        "name": "Goodies",
        "description": "Distribute goodies",
        "event": {
            "eventId": 1,
            "eventName": "Waterpolo quarter-finals",
            "timeSlot": {
                "start": today.toISOString(),
                "end": tomorrow.toISOString()
            },
            "place": {
                "id": 2,
                "name": "Bercy Sports Palace",
                "description": "Indoor sports complex",
                "street": "Boulevard de Bercy",
                "number": "8",
                "city": "Paris",
                "zip": "75012",
                "parking": false,
                "latitude": 48.8365,
                "longitude": 2.3738
            }
        }
    },
    {
        "id": 2,
        "name": "Opening",
        "description": "Welcome participants",
        "event": {
            "eventId": 1,
            "eventName": "Waterpolo quarter-finals",
            "timeSlot": {
                "start": today.toISOString(),
                "end": tomorrow.toISOString()
            },
            "place": {
                "id": 2,
                "name": "Bercy Sports Palace",
                "description": "Indoor sports complex",
                "street": "Boulevard de Bercy",
                "number": "8",
                "city": "Paris",
                "zip": "75012",
                "parking": false,
                "latitude": 48.8365,
                "longitude": 2.3738
            }
        }
    },
    {
        "id": 3,
        "name": "Check timing system",
        "description": "Verify timing devices",
        "event": {
            "eventId": 2,
            "eventName": "Waterpolo demi-finals",
            "timeSlot": {
                "start": tomorrow.toISOString(),
                "end": tomorrowTomorrow.toISOString()
            },
            "place": {
                "id": 2,
                "name": "Bercy Sports Palace",
                "description": "Indoor sports complex",
                "street": "Boulevard de Bercy",
                "number": "8",
                "city": "Paris",
                "zip": "75012",
                "parking": false,
                "latitude": 48.8365,
                "longitude": 2.3738
            }
        }
    },
    {
        "id": 4,
        "name": "Clean track",
        "description": "Clean the confettis after the race",
        "event": {
            "eventId": 2,
            "eventName": "Waterpolo demi-finals",
            "timeSlot": {
                "start": tomorrow.toISOString(),
                "end": tomorrowTomorrow.toISOString()
            },
            "place": {
                "id": 2,
                "name": "Bercy Sports Palace",
                "description": "Indoor sports complex",
                "street": "Boulevard de Bercy",
                "number": "8",
                "city": "Paris",
                "zip": "75012",
                "parking": false,
                "latitude": 48.8365,
                "longitude": 2.3738
            }
        }
    }
]

export const emptyAgenda = []