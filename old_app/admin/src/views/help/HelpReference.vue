<template>
  <q-page class="q-ma-lg">
    <q-card class="bg-transparent no-shadow no-border">
      <q-card-section class="q-py-lg">
          <q-card class="bg-transparent no-shadow q-mb-xl">
            <div class="text-h4">Lab in Light v0</div>
          </q-card>

          <q-card class="q-mb-xl">
            <q-card-section class="q-pb-none">
              <q-item>
                <q-item-section avatar>
                  <q-icon name="download" />
                </q-item-section>

                <q-item-section>
                  <q-item-label class="text-h6">Releases:</q-item-label>
                  <q-item-label caption>(Tested on Windows 10)</q-item-label>
                </q-item-section>
              </q-item>
            </q-card-section>
            <q-card-section class="q-pt-none q-pb-lg">
              <q-list separator padding class="rounded-borders">
                <q-item
                  clickable
                  v-ripple
                  tag="a"
                  target="_blank"
                  href="https://www.mediafire.com/folder/4vafh56qde8fc/LabinLight_Releases"
                >
                  <q-item-section>
                    Mediafire
                    <q-item-label caption>(certificate valid until 17 February 2026, 13:50:10)</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-card-section>
          </q-card>

          <q-card class="q-mb-xl">
            <q-card-section class="q-pb-none">
              <q-item>
                <q-item-section avatar>
                  <q-icon name="install_desktop" />
                </q-item-section>

                <q-item-section>
                  <q-item-label class="text-h6">Requirements:</q-item-label>
                </q-item-section>
              </q-item>
            </q-card-section>
            <q-card-section class="q-pt-none q-pb-lg">
              <q-list separator padding class="rounded-borders">
                <q-item
                  clickable
                  v-ripple
                  tag="a"
                  target="_blank"
                  href="https://learn.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist?view=msvc-170#visual-studio-2010-vc-100-sp1-no-longer-supported"
                >
                  <q-item-section>
                    Visual Studio 2010 (VC++ 10.0) SP1 (x86)
                  </q-item-section>
                </q-item>

                <q-item
                  clickable
                  v-ripple
                  tag="a"
                  target="_blank"
                  href="https://dotnet.microsoft.com/en-us/download/dotnet-framework/net472"
                >
                  <q-item-section>
                    .NET Framework 4.7.2
                    <q-item-label caption>(the OS should automatically prompt to install if needed)</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-card-section>
          </q-card>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<style lang="scss" scoped>
.chart-holder {
  max-height: 100px;
}
</style>
