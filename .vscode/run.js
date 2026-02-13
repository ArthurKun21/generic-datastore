const { execSync } = require("child_process");
const cwd = require("path").resolve(__dirname, "..");
const gradlew = process.platform === "win32" ? ".\\gradlew.bat" : "./gradlew";
const args = process.argv.slice(2).join(" ");
try {
  execSync(`${gradlew} ${args}`, { stdio: "inherit", cwd });
} catch (e) {
  process.exit(e.status || 1);
}
