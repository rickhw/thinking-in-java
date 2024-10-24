class Person {
    constructor(name, age) {
      this.name = name;
      this.age = age;
    }
  }
  
  const person = new Person('Alice', 30);
  console.log(`${person.name} is ${person.age} years old.`);