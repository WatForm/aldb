package commands;

import commands.EmptyCommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

public class TestEmptyCommand {
    private final EmptyCommand empty = new EmptyCommand();

    @Test
    public void testGetName() {
        assertNull(empty.getName());
    }

    @Test
    public void testGetDescription() {
        assertNull(empty.getDescription());
    }

    @Test
    public void testGetHelp() {
        assertNull(empty.getHelp());
    }

    @Test
    public void testRequiresFile() {
        assertFalse(empty.requiresFile());
    }
}
