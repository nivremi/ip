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
Here are the tasks in your list:
1.[T][ ] read book
2.[T][ ] return book
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```

## Test case: Preserve state for empty-list, repeated-status, and overflow operations

**Aim:** Verify that invalid operations leave an empty list unchanged, repeated status commands do not duplicate tasks, deleting a completed task works, and an oversized task number does not crash or mutate the list.

**Input:**

```text
delete 1
mark 1
unmark 1
list
todo alpha
mark 1
mark 1
list
delete 1
list
todo beta
unmark 1
unmark 1
delete 999999999999999999999
list
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
Task 1 does not exist.
Let's try that again!
------------------------------------------------------------
Task 1 does not exist.
Let's try that again!
------------------------------------------------------------
Task 1 does not exist.
Let's try that again!
------------------------------------------------------------
Yay! You have completed all your tasks
------------------------------------------------------------
Okay, I've added: [T][ ] alpha
You have a total of 1 tasks in the list.
------------------------------------------------------------
Alright! I have set it to done! Good Work!

[T][X] alpha
------------------------------------------------------------
Alright! I have set it to done! Good Work!

[T][X] alpha
------------------------------------------------------------
Here are the tasks in your list:
1.[T][X] alpha
------------------------------------------------------------
Gotcha, I will remove this task from your list:
  [T][X] alpha
Now you have 0 tasks in the list.
------------------------------------------------------------
Yay! You have completed all your tasks
------------------------------------------------------------
Okay, I've added: [T][ ] beta
You have a total of 1 tasks in the list.
------------------------------------------------------------
Got it! I have set it to not done!

[T][ ] beta
------------------------------------------------------------
Got it! I have set it to not done!

[T][ ] beta
------------------------------------------------------------
Try a number from 1 to 1!
------------------------------------------------------------
Here are the tasks in your list:
1.[T][ ] beta
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```

## Test case: Handle whitespace and case-sensitive commands without corrupting state

**Aim:** Verify that leading, trailing, and repeated spaces are accepted while an unsupported command casing does not add a task.

**Input:**

```text
  todo   spaced task  
TODO uppercase task
list
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
Okay, I've added: [T][ ] spaced task
You have a total of 1 tasks in the list.
------------------------------------------------------------
I'm sorry, I don't know what is 'TODO'. Try todo, deadline, event, list, delete, mark, unmark, or bye.
------------------------------------------------------------
Here are the tasks in your list:
1.[T][ ] spaced task
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```

## Test case: Reject malformed deadline and event markers

**Aim:** Verify that `/by`, `/from`, and `/to` must be standalone markers and malformed input does not add a task.

**Input:**

```text
deadline task /byFriday
event meeting /fromMonday /to Tuesday
list
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
A deadline needs a description and a by date. 
Try: deadline {your task} /by {deadline}
------------------------------------------------------------
An event needs a description, start, and end time. 
Try: event {task} /from {start} /to {end}
------------------------------------------------------------
Yay! You have completed all your tasks
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```

## Test case: Delete tasks without corrupting task order

**Aim:** Verify that deletion removes the selected task, shifts later tasks into the correct positions, and rejects invalid deletion requests without changing the list.

**Input:**

```text
todo read book
deadline return book /by Friday
event project meeting /from Monday /to Tuesday
todo borrow book
delete 3
list
delete 3
list
delete 4
delete first
bye
```

**Expected output:**

```text
{{ANY_PREFIX}}
Okay, I've added: [T][ ] read book
You have a total of 1 tasks in the list.
------------------------------------------------------------
Okay, I've added: [D][ ] return book (by: Friday)
You have a total of 2 tasks in the list.
------------------------------------------------------------
Okay, I've added: [E][ ] project meeting (from: Monday to: Tuesday)
You have a total of 3 tasks in the list.
------------------------------------------------------------
Okay, I've added: [T][ ] borrow book
You have a total of 4 tasks in the list.
------------------------------------------------------------
Gotcha, I will remove this task from your list:
  [E][ ] project meeting (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
------------------------------------------------------------
Here are the tasks in your list:
1.[T][ ] read book
2.[D][ ] return book (by: Friday)
3.[T][ ] borrow book
------------------------------------------------------------
Gotcha, I will remove this task from your list:
  [T][ ] borrow book
Now you have 2 tasks in the list.
------------------------------------------------------------
Here are the tasks in your list:
1.[T][ ] read book
2.[D][ ] return book (by: Friday)
------------------------------------------------------------
Task 4 does not exist.
Let's try that again!
------------------------------------------------------------
The task number must be a whole number. Try: delete 1
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
Here are the tasks in your list:
1.[T][ ] alpha
------------------------------------------------------------
Alright! I have set it to done! Good Work!

[T][X] alpha
------------------------------------------------------------
Task 3 does not exist.
Let's try that again!
------------------------------------------------------------
Here are the tasks in your list:
1.[T][X] alpha
------------------------------------------------------------
Got it! I have set it to not done!

[T][ ] alpha
------------------------------------------------------------
The task number must be at least 1. Try: mark 1
Let's try that again!
------------------------------------------------------------
Here are the tasks in your list:
1.[T][ ] alpha
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
Here are the tasks in your list:
1.[D][ ] return book (by: Friday)
2.[E][ ] meeting (from: Monday to: Tuesday)
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
Please provide a task number! Try: mark {task no.}
------------------------------------------------------------
The task number must be a whole number! Try: mark {task no.}
------------------------------------------------------------
Task 1 does not exist.
Let's try that again!
------------------------------------------------------------
The task number must be at least 1. Try: unmark 1
Let's try that again!
------------------------------------------------------------
The list command does not take any extra text. Try: list
------------------------------------------------------------
The bye command does not take any extra text. Try: bye
------------------------------------------------------------
I'm sorry, I don't know what is 'blah'. Try todo, deadline, event, list, delete, mark, unmark, or bye.
------------------------------------------------------------
Bye! Hope to see you again soon!
------------------------------------------------------------
```
