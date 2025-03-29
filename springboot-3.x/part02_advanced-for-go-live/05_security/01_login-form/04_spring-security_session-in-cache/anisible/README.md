

```bash
# update ubuntu
ansible-playbook -i hosts.ini update_ubuntu.yml --ask-pass --ask-become-pass

# update app
ansible-playbook -i hosts-gtapp.ini deploy_spring_app.yml --ask-pass
``