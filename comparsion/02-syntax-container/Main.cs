public record Person(string Name, int Age);

public class Program {
    public static void Main() {
        var person = new Person("Alice", 30);
        Console.WriteLine($"{person.Name} is {person.Age} years old.");
    }
}