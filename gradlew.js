const { spawn } = require('child_process');

const isWindows = process.platform === 'win32';
const gradlew = isWindows ? 'gradlew.bat' : './gradlew';

const args = process.argv.slice(2);

const child = spawn(gradlew, args, { stdio: 'inherit', shell: true });

child.on('close', (code) => {
    process.exit(code);
});