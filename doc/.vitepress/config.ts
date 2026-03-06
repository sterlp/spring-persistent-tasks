import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "Spring Persistent Tasks",
  description: "A lightweight task management framework for Spring Boot with JPA persistence, clustering support, and retry mechanisms for asynchronous task execution",
  srcDir: './docs',
  base: '/',
  lastUpdated: true,
  cleanUrls: true,
  ignoreDeadLinks: true,

  themeConfig: {
    siteTitle: "Spring Persistent Tasks",
    logo: '/assets/logo.png',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Setup', link: '/setup/maven-setup' },
      { text: 'Test', link: '/test/junit-test' }
    ],

    sidebar: {
      '/': [
        {
          text: 'Introduction',
          items: [
            { text: 'Overview', link: '/' }
          ]
        },
        {
          text: 'Setup',
          items: [
            { text: 'Maven Setup', link: '/setup/maven-setup' },
            { text: 'Liquibase Setup', link: '/setup/liquibase-setup' },
            { text: 'Spring Configuration Options', link: '/setup/spring-configuration-options' },
            { text: 'Scheduler Name', link: '/setup/scheduler-name' },
            { text: 'CSRF Dashboard UI', link: '/setup/csrf-daschboard-ui' },
            { text: 'Customize Serialization', link: '/setup/cusomize-serialization' }
          ]
        },
        {
          text: 'Tasks',
          items: [
            { text: 'Register Spring Task', link: '/tasks/register-spring-task' },
            { text: 'Queue a Spring Task', link: '/tasks/queue-a-spring-task' },
          ]
        },
        {
          text: 'Task Triggers',
          items: [
            { text: 'Cron Triggers', link: '/trigger/cron-triggers' },
            { text: 'Delete Task Trigger', link: '/trigger/delete-task-trigger' },
            { text: 'Failed Spring Triggers', link: '/trigger/failed-spring-triggers' }
          ]
        },
        {
          text: 'Test',
          items: [
            { text: 'JUnit Test', link: '/test/junit-test' }
          ]
        },
        {
          text: 'Additional Topics',
          items: [
            { text: 'Life Cycle Events', link: '/life-cycle-events' },
            { text: 'Transaction Management', link: '/transaction-management' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/sterlp/spring-persistent-tasks' }
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2024 Paul Sterl'
    },

    search: {
      provider: 'local',
      options: {
        detailedView: true
      }
    }
  },

  markdown: {
    theme: 'material-theme-palenight',
    lineNumbers: true
  }
})