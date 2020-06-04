install_fluent_logger:
  cmd.run:
    - name: pip install fluent-logger>=0.9.6' --ignore-installed
    - unless: pip list --no-index | grep -E 'fluent-logger'

install_yaml:
  cmd.run:
    - name: pip install PyYAML --ignore-installed
    - unless: pip list --no-index | grep -E 'PyYAML'

install_pid:
  cmd.run:
    - name: pip install pid --ignore-installed
    - unless: pip list --no-index | grep -E 'pid'