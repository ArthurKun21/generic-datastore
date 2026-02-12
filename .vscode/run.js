const { execSync } = require("child_process");
const args = process.argv.slice(2).join(" ");
try {
  execSync(args, { stdio: "inherit", cwd: __dirname + "/.." });
} catch (e) {
  process.exit(e.status || 1);
}
