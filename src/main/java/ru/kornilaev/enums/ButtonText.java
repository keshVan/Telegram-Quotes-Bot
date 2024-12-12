package ru.kornilaev.enums;

public enum ButtonText {
    ADD_IN_FAVORITES(":star: В избранное"),
    REMOVE_FROM_FAVORITES(":x: Удалить из избранного"),
    PREVIOUS(":arrow_backward:"),
    NEXT(":arrow_forward:"),
    STAR(":star:"),
    X_MARK(":x:");

    private final String value;

    ButtonText(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}