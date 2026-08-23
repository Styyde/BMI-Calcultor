import { describe, expect, it } from "vitest";
import { getCategoryColor, getCategoryLabel } from "./category-colors";

describe("getCategoryColor", () => {
  it("returns the known color for a category", () => {
    expect(getCategoryColor("NORMAL")).toBe("#2ecc71");
  });

  it("prefers an explicit fallback over the lookup table", () => {
    expect(getCategoryColor("NORMAL", "#000000")).toBe("#000000");
  });

  it("returns a neutral default for an unknown category", () => {
    expect(getCategoryColor("SOMETHING_ELSE")).toBe("#94a3b8");
  });
});

describe("getCategoryLabel", () => {
  it("returns the known label for a category", () => {
    expect(getCategoryLabel("OBESE_CLASS_1")).toBe("Obese (Class I)");
  });

  it("humanizes an unknown category by replacing underscores", () => {
    expect(getCategoryLabel("SOME_UNKNOWN_CATEGORY")).toBe("SOME UNKNOWN CATEGORY");
  });
});
