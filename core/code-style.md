## General rules

- Clean code apply
- Uncle Bob SOLID principle should be considered where useful
- if a method has more than 4 parameters consider an value/ entity class
- the zalando REST API rules apply
- run maven clean insall before a commit / end of work

## Testing

Each major functionality should be tested each test should be structured with. One test for each class with is called `subject` in the test to make clear this is the tested component.
Test should try to work with random data, so a clear shoud in general not be required.

// GIVEN
here is all the setup code and mybe any required special clear code for this test.

// WHEN
here is alle the action / modification code calling the service

// THEN
here comes all the assert code

## Component architecture

Each component is in its own package. Any access to a component has to be through the *Services classes - which are in the root directory of the folder.

All classes despite Model/Entity classes or converter are considered private and should not be used by any other component.
Each component is itself implemented by the help of

### Services classes e.g. PersonService
Position: Root Packge of the component

Represent the public internal API between components. Is usually annotated with @Service

- Implementiert den „Workflow“
- High Level Logik
- Aspekte wie
    - Transaktions-management
    - Autorisierung
    - Caching
    - Policies

Has usually no private methods. Private method are implemented by components.
A service should have a short description in the java doc - where its responsibility is state clearly to ensure no other component or services does the same.

Method names are more generic as the one used in repositories or components.
A Service may call other services.
A Service may call other components or repositories in their module/component.

### Component classes e.g. DeletePersonComponent

Position: sub package "component"

Implementiert den „Use-Case/ Step“. Usually named like the method itself. In general has only one responsibility and usually only one "execute" method. May have serveral methods. but always to access this one logic in different ways.
Otherwise the method names are very specific.

Low Level Logik
Wie eine private Methode im Service
Sollte alleine testbar sein

- A component may call other components in their module/component.
- As a component is a low lever building part of the application it is not allowed to delegate the work up to the services of its own component.
- But a component may call other functionality from other components through foreign Services classes.

### Repository classes e.g. PersonRepository
Position: sub package "repository"

- Implementiert die „Persistenz“
- Abstraktion der Datenbank
- Queries

### Resource class e.g. PersonResource

Position: sub package "api"

Implements the public interface and delegates to the service. It it the anty corruption layer to the ourside world, it has no business logic and only converts from the external API to the internal entity classes.

### Entity class e.g. PersonEntity
Position: sub package "model"

Represent the "DTO" or "DB Entities" of the components. They are suffixed with "Entity" to ensure no name collision with the API classes, which has no suffix.
These classes can be shared between components but any modification or creation to this class has to be done by the Service owning this entity.

### Timers e.g. PersonTimer

Position: either root package with the service or sub package "timer"

A Timer should only be an internal way to call the service. Itself has no logic despite logging. Any logic sould be in the Service or if it is more complex in an Component class, or several, which is/are used by the Service.