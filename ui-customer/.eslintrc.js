module.exports = {
  root: true,
  overrides: [
    {
      files: ["*.ts"],
      parserOptions: {
        project: [
          "tsconfig.*?.json",
          "e2e/tsconfig.json"
        ],
        createDefaultProgram: true
      },
      extends: [
        "plugin:@angular-eslint/recommended",
        "eslint:recommended",
        "plugin:@typescript-eslint/recommended",
        'airbnb-typescript/base',
        // Settings for Prettier
        'prettier',
        "plugin:prettier/recommended"
      ],
      rules: {
        "no-console": "off",
        "no-plusplus": "off",
        "prefer-destructuring":"off",
        "no-param-reassign":"off",
        "no-restricted-syntax":"off",
        "no-underscore-dangle":"off",
        'import/no-unresolved': 'off',
        'import/prefer-default-export': 'off',
        'class-methods-use-this': 'off',
        'lines-between-class-members': 'off',
        '@typescript-eslint/unbound-method': [
          'error',
          {
            ignoreStatic: true,
          }
        ],
        "@typescript-eslint/explicit-module-boundary-types": "off",
        "@typescript-eslint/no-explicit-any": "off",
        "@typescript-eslint/ban-types":"off",
        "no-restricted-properties":"off",
        "prettier/prettier": ["error", {
         "endOfLine":"auto",
         "trailingComma": "none",
         "singleQuote": false,
         "useTabs": false
       }]
      }
    },
    {
      files: ["*.html"],
      extends: [
        "plugin:@angular-eslint/template/recommended",
        //"plugin:prettier/recommended"
      ],
      rules: {
        "max-len": ["error", { "code": 500 }]
      }
    },
    {
      files: ["*.component.ts"],
      extends: ["plugin:@angular-eslint/template/process-inline-templates"]
    }
  ]
}
