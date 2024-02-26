package io.amoakoagyei.marketplace;

import io.amoakoagyei.Command;

public record CreateAdCommand(String title) implements Command {
}
