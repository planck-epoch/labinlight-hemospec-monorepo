module.exports = {
  devServer: {
    proxy: 'http://127.0.0.1:4000/',
    public: 'http://127.0.0.1:4000/',
  },
  pluginOptions: {
    quasar: {
      treeShake: true
    },
    apollo: {
      cors: '*',
    }
  },
  transpileDependencies: [
    /[\\\/]node_modules[\\\/]quasar[\\\/]/
  ],
  chainWebpack: config => {
    config
    .plugin('html')
    .tap(args => {
      args[0].title = "Lab in Light - Admin";
      return args;
    })
  }
}
