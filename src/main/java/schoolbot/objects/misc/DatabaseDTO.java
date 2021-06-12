package schoolbot.objects.misc;

/**
 * Used to transfer data between class when updating classes, schools, assignments, etc
 */
public record DatabaseDTO(Object objectBeingUpdated, String updateColumn, Object valueBeingChanged)
{
}
