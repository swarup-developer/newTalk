export const manifest = {
  id: 'example-plugin',
  name: 'Example plugin',
  version: '1.0',
  author: 'newTalk',
  homepage: 'https://github.com/swarup-developer/newTalk/blob/master/docs/scripting/examples/example-plugin.js',
  description: 'Says the name of each app you switch to, and counts how often you open each one.',
  // screen: a script for all apps is told which app is in front. speech: newtalk.speak.
  permissions: ['screen', 'speech'],
  settings: [
    {
      key: 'sayApp',
      type: 'switch',
      title: 'Say the app when you switch apps',
      default: true,
    },
    {
      key: 'sayCount',
      type: 'switch',
      title: 'Also say how often you opened it',
      default: false,
    },
  ],
};

newtalk.onAppEnter((app) => {
  if (!app.package) return;
  const counts = newtalk.storage.get('counts', {});
  counts[app.package] = (counts[app.package] || 0) + 1;
  newtalk.storage.set('counts', counts);

  if (!newtalk.settings.get('sayApp')) return;
  const name = app.package.split('.').pop();
  const opened = counts[app.package];
  const count = newtalk.settings.get('sayCount')
    ? `, opened ${opened} ${opened === 1 ? 'time' : 'times'}`
    : '';
  newtalk.speak(`${name}${count}`);
});

console.log('Example plugin loaded');
