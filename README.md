# Airplane Simulation

A Java Swing desktop app for placing airports on a world map, connecting them with scheduled flights, and watching the aircraft depart, cross the map, and land.

You add airports by three-letter code and coordinates, link them with flights, then start the clock. The engine queues departures per airport, moves each aircraft along its route, and marks it landed once the flight duration runs out.

## Screens

**Data tab.** Two tables, one for airports and one for flights. Add and remove rows here. Both forms validate input before the record reaches the model.

**Map & Simulation tab.** A 720x360 pixel plot of the world, where x maps to longitude and y to latitude. Gray squares mark airports, blue dots mark aircraft in flight. Click a square to select that airport and it blinks red until you click it again. The checkbox list on the right hides airports you do not want drawn.

## Requirements

JDK 8 or newer. The build needs nothing else: no Maven, no Gradle, no third-party jars.

## Build and run

```bash
javac -d out $(find src -name "*.java")
```

```bash
java -cp out Main
```

On Windows PowerShell:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
```

## Data files

`File > Save as CSV`, `File > Save as JSON`, and `File > Load from file` handle persistence. `DataManager` chooses the parser from the file extension and rejects anything that is not `.csv` or `.json`.

### CSV

Section markers and column headers are required, and both sections must appear in this order:

```csv
# AIRPORTS
CODE,NAME,X,Y
BEG,Belgrade Nikola Tesla,20,44
CDG,Paris Charles de Gaulle,2,49
# FLIGHTS
FROM,TO,DEPARTURE,DURATION
BEG,CDG,08:30,140
```

Airport names cannot contain a comma. `CsvFormat` throws `FileException` on save instead of writing a row that will fail to parse back.

### JSON

```json
{
  "airports": [
    { "code": "BEG", "name": "Belgrade Nikola Tesla", "x": 20, "y": 44 },
    { "code": "CDG", "name": "Paris Charles de Gaulle", "x": 2, "y": 49 }
  ],
  "flights": [
    { "from": "BEG", "to": "CDG", "departure": "08:30", "duration": 140 }
  ]
}
```

`JsonFormat` uses a hand-written parser. It accepts the shape above and rejects unknown fields.

## Validation rules

| Field | Rule |
|---|---|
| Airport code | Three uppercase letters, unique across the data set |
| Airport name | Non-empty, no comma when saving to CSV |
| X (longitude) | Integer from -180 to 180 |
| Y (latitude) | Integer from -90 to 90 |
| Departure | `HH:MM`, hours 0-23, minutes 0-59 |
| Duration | Positive integer, in minutes |
| Flight endpoints | Departure and arrival airports must differ |

`Validator` enforces these rules, and the `Airport` and `Flight` constructors check them a second time. Form entry and file import share one code path.

## Simulation model

`SimulationEngine` runs a daemon thread with a fixed accumulator. Each 200 ms tick advances the clock two simulated minutes.

`DepartureScheduler` gives every airport a runway slot of ten simulated minutes. Flights competing for the same airport queue by scheduled departure and leave one per slot, so a busy airport spreads its traffic across slots. An aircraft that waits past its scheduled time takes off at the next free slot.

Each flying aircraft interpolates between its two airports and reaches state `LANDED` once elapsed time passes the flight duration.

Start, Pause, Resume, and Reset sit above the map. Adding airports and flights stays disabled while a simulation runs.

## Auto-close on idle

The app exits after 60 seconds without mouse or keyboard input. A dialog warns you for the last 5 seconds, and the status bar counts down the whole time. **You lose unsaved work when the timer fires.** Save before you step away from the keyboard.

Two things suspend the timer: selecting an airport on the map, and running the simulation. The status bar reads `Auto-close: paused` while either one holds.

To change the timeout, edit `TIMEOUT_MILLIS` in `src/tools/InactivityMonitor.java`.

## Layout

```
src/
  Main.java        Entry point, hands off to the Swing thread
  model/           Airport, Flight, Aircraft, Time, and the Data store
  io/              DataManager plus the CSV and JSON formats
  gui/             MainFrame and the panels it hosts
  simulation/      SimulationEngine and DepartureScheduler
  tools/           Validator and InactivityMonitor
  listeners/       Data, simulation, and inactivity callbacks
  exceptions/      AppException and its three subclasses
```

`Data` holds the airports and flights, then notifies every registered `DataListener` after a change. Panels redraw from those callbacks, so no panel talks to another panel.

## Known limits

- `Data.getAirports()` and `getFlights()` hand out the live internal lists, so a caller can add a record that skipped validation.
- The JSON parser copies the whole file into memory before parsing, and it reads `\uXXXX` escapes as the literal character `u`.
- Coordinates are integers, so airports snap to whole degrees.
- `MainFrame.simulationError` discards the message it receives.
- The project has no test suite and no build file.
