module.exports = {
    root: true,
    env: {
        es6: true,
        node: true,
    },
    extends: [
        "eslint:recommended",
        "plugin:import/errors",
        "plugin:import/warnings",
        "plugin:import/typescript",
        "google",
    ],
    parser: "@typescript-eslint/parser",
    parserOptions: {
        project: ["tsconfig.json", "tsconfig.dev.json"],
        sourceType: "module",
    },
    ignorePatterns: [
        "/lib/**/*", // Ignore built files.
        "/generated/**/*", // Ignore generated files.
    ],
    plugins: [
        "@typescript-eslint",
        "import",
    ],
    rules: {
        "quotes": ["error", "double"],
        "import/no-unresolved": 0,
        // Disable the indent rule
        "indent": "off", // or "indent": 0
        // Disable the max-len rule (ESLint's default, or if inherited from 'google' preset)
        "max-len": "off", // or "max-len": 0
        // If @typescript-eslint/indent is active (often it is if you extend @typescript-eslint/recommended),
        // you might also need to disable its version of indent:
        "@typescript-eslint/indent": "off", // or "@typescript-eslint/indent": 0
    },
};
