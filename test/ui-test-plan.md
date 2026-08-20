# UI Test Plan

## Program command

```text
java -cp build Rei
```

The command assumes the project has been compiled into `build` with Java 25.

## Test case: Add tasks and list them

**Aim:** Verify that entered task text is stored and later displayed in entry order.

**Input:**

```text
todo read book
todo return book
list
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
------------------------------------------------------------
Okay, I've added: [T][ ] read book
You have a total of 1 tasks in the list.
------------------------------------------------------------
Okay, I've added: [T][ ] return book
You have a total of 2 tasks in the list.
------------------------------------------------------------
[T][ ] read book
[T][ ] return book
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```

## Test case: List an empty task collection

**Aim:** Verify that `list` does not display task entries before any task has been added.

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
Yay! You have completed all your tasks
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```
