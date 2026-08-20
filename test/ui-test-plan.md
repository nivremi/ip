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

## Test case: Preserve task state after rejected task and status commands

**Aim:** Verify that invalid commands do not add tasks or change an existing task's completion status.

**Input:**

```text
todo alpha
todo
list
mark 1
unmark 3
list
unmark 1
mark 0
list
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
Okay, I've added: [T][ ] alpha
You have a total of 1 tasks in the list.
------------------------------------------------------------
A todo needs a description. 
Try: todo {your task}
------------------------------------------------------------
[T][ ] alpha
------------------------------------------------------------
Alright! I have set it to done! Good Work!

[T][X] alpha
------------------------------------------------------------
Task 3 does not exist. Try a number from 1 to 1.
Let's try that again!
------------------------------------------------------------
[T][X] alpha
------------------------------------------------------------
Got it! I have set it to not done!

[T][ ] alpha
------------------------------------------------------------
The task number must be at least 1. Try: mark 1
Let's try that again!
------------------------------------------------------------
[T][ ] alpha
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```

## Test case: Preserve task state after malformed deadline and event commands

**Aim:** Verify that invalid scheduled-task formats do not affect later valid tasks or the task count.

**Input:**

```text
deadline /by Friday
deadline return book /by Friday
event meeting /from Monday
event meeting /from Monday /to Tuesday
deadline submit report /by
list
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
A deadline needs a description and a by date. 
Try: deadline {your task} /by {deadline}
------------------------------------------------------------
Okay, I've added: [D][ ] return book (by: Friday)
You have a total of 1 tasks in the list.
------------------------------------------------------------
An event needs a description, start, and end time. 
Try: event {task} /from {start} /to {end}
------------------------------------------------------------
Okay, I've added: [E][ ] meeting (from: Monday to: Tuesday)
You have a total of 2 tasks in the list.
------------------------------------------------------------
A deadline needs a description and a by date. 
Try: deadline {your task} /by {deadline}
------------------------------------------------------------
[D][ ] return book (by: Friday)
[E][ ] meeting (from: Monday to: Tuesday)
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

## Test case: Explain invalid commands and task details

**Aim:** Verify that invalid input is handled without crashing and each message explains how to correct it.

**Input:**

```text

todo
deadline read book
event meeting /from Monday
mark
mark first
mark 1
unmark 0
list extra
bye later
blah
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
Please enter a command. Try: todo read book
------------------------------------------------------------
A todo needs a description. 
Try: todo {your task}
------------------------------------------------------------
A deadline needs a description and a by date. 
Try: deadline {your task} /by {deadline}
------------------------------------------------------------
An event needs a description, start, and end time. 
Try: event {task} /from {start} /to {end}
------------------------------------------------------------
Please provide a task number! Try: mark {task no.}read
------------------------------------------------------------
The task number must be a whole number! Try: mark {task no.}
------------------------------------------------------------
Task 1 does not exist. Try a number from 1 to 0.
Let's try that again!
------------------------------------------------------------
The task number must be at least 1. Try: unmark 1
Let's try that again!
------------------------------------------------------------
The list command does not take any extra text. Try: list
------------------------------------------------------------
The bye command does not take any extra text. Try: bye
------------------------------------------------------------
I'm sorry, I don't know what is 'blah'. Try todo, deadline, event, list, mark, unmark, or bye.
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```
