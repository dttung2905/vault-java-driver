package io.github.jopenlibs.vault.util;

public class VaultVersion implements Comparable<VaultVersion> {

    private final String literal;

    private final int[] numbers;

    public VaultVersion(String version) {
        this.literal = version;

        final String[] split = version.split("\\-")[0].split("\\.");
        this.numbers = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            try {
                numbers[i] = Integer.valueOf(split[i]);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Not a semver version");
            }
        }
    }

    public String getLiteral() {
        return this.literal;
    }

    public int[] getNumbers() {
        return this.numbers;
    }

    @Override
    public int compareTo(VaultVersion another) {
        final int maxLength = Math.max(this.getNumbers().length, another.getNumbers().length);
        for (int i = 0; i < maxLength; i++) {
            final int left = i < this.getNumbers().length ? this.getNumbers()[i] : 0;
            final int right = i < another.getNumbers().length ? another.getNumbers()[i] : 0;
            if (left != right) {
                return left < right ? -1 : 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return this.getLiteral();
    }
}
