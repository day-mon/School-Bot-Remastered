package schoolbot.objects.misc;


import java.time.LocalDateTime;

public record Reminder(int id, Object obj, LocalDateTime... time)
{
}
