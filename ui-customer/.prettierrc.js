module.exports = {
  tabWidth: 4,
  useTabs: true,
  semi: true,
  singleQuote: true,
  quoteProps: "as-needed",
  trailingComma: "none",
  bracketSpacing: true,
  arrowParens: "always",
  endOfLine:"auto",
  overrides: [
      {
          "files": "*.component.html",
          "options": {
              "parser": "angular"
          }
      },
      {
          "files": "*.html",
          "options": {
              "parser": "html"
          }
      }
  ]
};
