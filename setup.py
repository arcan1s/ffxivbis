from distutils.util import convert_path
from setuptools import setup, find_packages
from os import path


here = path.abspath(path.dirname(__file__))
metadata = dict()
with open(convert_path('src/service/core/version.py')) as metadata_file:
    exec(metadata_file.read(), metadata)


setup(
    name='ffxivbis',

    version=metadata['__version__'],
    zip_safe=False,

    description='Helper to handle loot drop',

    author='Evgeniy Alekseev',
    author_email='i@arcanis.me',

    license='BSD',

    packages=find_packages(exclude=['contrib', 'docs', 'tests']),

    install_requires=[
        'aiohttp',
        'requests',
        'yoyo_migrations'
    ],
    setup_requires=[
        'pytest-runner'
    ],
    tests_require=[
        'pytest', 'pytest-aiohttp'
    ],

    include_package_data=True,

    extras_require={
        'test': ['coverage', 'pytest'],
    },
)
